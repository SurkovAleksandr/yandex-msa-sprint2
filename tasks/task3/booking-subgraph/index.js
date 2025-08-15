import {ApolloServer} from '@apollo/server';
import {startStandaloneServer} from '@apollo/server/standalone';
import {buildSubgraphSchema} from '@apollo/subgraph';
import gql from 'graphql-tag';
import {ForbiddenError} from 'apollo-server-errors';

import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';

const PROTO_PATH = './booking.proto';

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});

const bookingProto = grpc.loadPackageDefinition(packageDefinition).booking;

const client = new bookingProto.BookingService(
    'booking-service:9090',
    grpc.credentials.createInsecure()
);

const typeDefs = gql`

  extend type Hotel @key(fields: "id") {
    id: ID! @external
  }
  
  type Booking @key(fields: "id") {
    id: ID!
    userId: String!
    hotelId: String!
    promoCode: String
    discountPercent: Float
    hotel: Hotel
  }

  type Query {
    bookingsByUser(userId: String!): [Booking]
  }

`;

const resolvers = {
  Query: {
    bookingsByUser: async (_, { userId }, {headerUserId}) => {
		// Реальный вызов к grpc booking-сервису + ACL
      console.info("bookingsByUser")
      console.info(headerUserId)

      if (userId !== headerUserId) {
        console.log(`Access denied for user ${headerUserId}`);
        throw new ForbiddenError(`Access denied for user ${headerUserId}`);
      }

      return new Promise((resolve, reject) => {
        console.log('BookingService.ListBookings userId:' + userId);
        client.ListBookings({ user_id: userId }, (error, response) => {
          if (error) {
            return reject(error);
          }

          // Преобразуем каждый booking
          const bookings = response.bookings.map(b => ({
            id: b.id,
            userId: b.user_id,
            hotelId: b.hotel_id,
            promoCode: b.promo_code,
            discountPercent: Math.floor(b.discount_percent),
          }));

          resolve(bookings);
        });
      });
    },
  },
  Booking: {
    hotel(booking) {
      return {
        __typename: "Hotel",
        id: booking.hotelId
      }
    }
  },
};

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }])
});

startStandaloneServer(server, {
  listen: { port: 4001 },
  /**
   * Пример определения контекста можно посмотреть [The contextValue argument](https://www.apollographql.com/docs/apollo-server/data/resolvers#the-contextvalue-argument)
   */
  context: async ({ req }) => {
    console.log(req.headers.userid)
    return {headerUserId: req.headers.userid}
  },
}).then(() => {
  console.log('✅ Booking subgraph ready at http://localhost:4001/');
});
