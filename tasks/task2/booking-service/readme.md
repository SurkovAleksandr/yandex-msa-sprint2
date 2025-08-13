После сборки проекта в виде jar файла можно собрать образ и запустить его:

```shell
docker build -t booking-service:1.0 .
```

```shell
docker run --rm -d \
  --name booking-service \
  -p 9090:9090 \
  --net=hotelio-net \
  booking-service:1.0
```