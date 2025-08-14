package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelio.proto.booking.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.util.Strings;
import org.example.entity.Booking;
import org.example.proxy.HotelFeignClient;
import org.example.proxy.PromoCodeFeignClient;
import org.example.proxy.ReviewFeignClient;
import org.example.proxy.UserFeignClient;
import org.example.proxy.dto.PromoCode;
import org.example.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GrpcService
public class BookingServiceImpl extends BookingServiceGrpc.BookingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private PromoCodeFeignClient promoCodeFeignClient;
    @Autowired
    private HotelFeignClient hotelFeignClient;
    @Autowired
    private ReviewFeignClient reviewFeignClient;
    @Autowired
    private KafkaProducerService kafkaProducerService;
    @Autowired
    private ObjectMapper objectMapper;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void createBooking(BookingRequest request, StreamObserver<BookingResponse> responseObserver) {
        String userId = request.getUserId();
        String hotelId = request.getHotelId();
        String promoCode = request.getPromoCode();

        log.info("Creating booking: userId={}, hotelId={}, promoCode={}", userId, hotelId, promoCode);

        validateUser(userId);
        validateHotel(hotelId);

        double basePrice = resolveBasePrice(userId);
        double discount = resolvePromoDiscount(promoCode, userId);

        double finalPrice = basePrice - discount;
        log.info("Final price calculated: base={}, discount={}, final={}", basePrice, discount, finalPrice);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setHotelId(hotelId);
        booking.setPromoCode(promoCode);
        booking.setDiscountPercent(discount);
        booking.setPrice(finalPrice);
        booking.setCreatedAt(Instant.now());

        Booking bookingCreated = bookingRepository.save(booking);

        log.info("Booking created: {}", bookingCreated);

        try {
            String bookingString = objectMapper.writeValueAsString(bookingCreated);
            kafkaProducerService.sendMessage(bookingString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        responseObserver.onNext(convertBookingToBookingResponse(bookingCreated));
        responseObserver.onCompleted();
    }

    @Override
    public void listBookings(BookingListRequest request, StreamObserver<BookingListResponse> responseObserver) {
        log.info("listBookings - userId: " + request.getUserId());

        List<Booking> bookings = request.getUserId() != null ? bookingRepository.findByUserId(request.getUserId()) : bookingRepository.findAll();

        log.info("Booking list: {}", bookings);

        List<BookingResponse> userBookings = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getUserId().equals(request.getUserId())) {
                userBookings.add(convertBookingToBookingResponse(b));
            }
        }

        BookingListResponse response = BookingListResponse.newBuilder()
                .addAllBookings(userBookings)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private BookingResponse convertBookingToBookingResponse(Booking booking) {
        return BookingResponse.newBuilder()
                .setId(booking.getId().toString())
                .setUserId(booking.getUserId())
                .setHotelId(booking.getHotelId())
                .setPromoCode(booking.getPromoCode() == null ? "null" : booking.getPromoCode())
                .setDiscountPercent(booking.getDiscountPercent())
                .setPrice(booking.getPrice())
                .setCreatedAt(booking.getCreatedAt().toString())
                .build();
    }

    private void validateUser(String userId) {
        if (!userFeignClient.isUserActive(userId)) {
            log.warn("User {} is inactive", userId);
            throw new IllegalArgumentException("User is inactive");
        }
        if (userFeignClient.isUserBlacklisted(userId)) {
            log.warn("User {} is blacklisted", userId);
            throw new IllegalArgumentException("User is blacklisted");
        }
    }

    private void validateHotel(String hotelId) {
        if (!hotelFeignClient.isOperational(hotelId)) {
            log.warn("Hotel {} is not operational", hotelId);
            throw new IllegalArgumentException("Hotel is not operational");
        }
        if (!reviewFeignClient.isHotelTrusted(hotelId)) {
            log.warn("Hotel {} is not trusted", hotelId);
            throw new IllegalArgumentException("Hotel is not trusted based on reviews");
        }
        if (hotelFeignClient.isFullyBooked(hotelId)) {
            log.warn("Hotel {} is fully booked", hotelId);
            throw new IllegalArgumentException("Hotel is fully booked");
        }
    }

    private double resolveBasePrice(String userId) {
        Optional<String> statusOpt = Optional.ofNullable(userFeignClient.getUserStatus(userId));
        return statusOpt.map(status -> {
            boolean isVip = status.equalsIgnoreCase("VIP");
            log.debug("User {} has status '{}', base price is {}", userId, status, isVip ? 80.0 : 100.0);
            return isVip ? 80.0 : 100.0;
        }).orElseGet(() -> {
            log.debug("User {} has unknown status, default base price 100.0", userId);
            return 100.0;
        });
    }

    private double resolvePromoDiscount(String promoCode, String userId) {
        if (Strings.isBlank(promoCode)) return 0.0;

        PromoCode promo = promoCodeFeignClient.validatePromo(promoCode, userId);
        if (promo == null) {
            log.info("Promo code '{}' is invalid or not applicable for user {}", promoCode, userId);
            return 0.0;
        }

        log.debug("Promo code '{}' applied with discount {}", promoCode, promo.getDiscount());
        return promo.getDiscount();
    }

    /**
     * Пример вызова:
     * grpcurl -plaintext -proto src/main/proto/booking.proto -d '{ "user_id": "123", "hotel_id": "hotelA", "promo_code": "PROMO10" }' localhost:9090 booking.BookingService/CreateBooking
     *
     */
    /*public static void main(String[] args) throws IOException, InterruptedException {
        int port = 9090;
        Server server = ServerBuilder.forPort(port)
                .addService(new BookingServiceImpl())
                //.addService(ProtoReflectionService.newInstance())
                .build()
                .start();

        System.out.println("gRPC server started on port " + port);
        server.awaitTermination();
    }*/
}
