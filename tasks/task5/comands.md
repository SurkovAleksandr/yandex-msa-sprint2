Работа делается на основе task4. Из task4 скопированы папки helm и booking-service.

## Установка istioctl
```shell
curl -L https://istio.io/downloadIstio | sh -
```

Добавить в PATH путь до istioctl
```shell
cd istio-*/bin
export PATH=$PWD:$PATH
```
или
```shell
export PATH=/home/u-user/istio-1.27.0/bin:$PATH
```


проверка, что istioctl установлено
!!! надо выполнить после предыдущих команд - куда установлен istioctl
```shell
istioctl version
```
установка в minikube
```shell
istioctl install --set profile=demo -y
```

Установка аддонов Istio — Prometheus, Grafana, Kiali, Jaeger:  
В каталоге установленного Istio есть примеры аддонов:
```shell
cd /home/u-user/istio-1.27.0
kubectl apply -f samples/addons/prometheus.yaml
kubectl apply -f samples/addons/grafana.yaml
kubectl apply -f samples/addons/kiali.yaml
kubectl apply -f samples/addons/jaeger.yaml
```

Проверка для каких namespace включен sidecar
```shell
kubectl get namespace --show-labels | grep istio-injection=enabled
```
```shell
kubectl get pods -n istio-system
```
Другой способ проверки включения sidecar
```shell
kubectl get namespace sprint2-task4 -o json | jq '.metadata.labels."istio-injection"'
```

включите инъекции istio в каждый под в неймспейсе. Делается для namespace sprint2-task4, т.к. используем наработки из task4.
```shell
kubectl label namespace sprint2-task4 istio-injection=enabled --overwrite
```
Дашборд kiali
```shell
istioctl dashboard kiali
```
Дашборд jaeger
```shell
istioctl dashboard jaeger
```

Для удобства дебага можно использовать access_log — в demo он включён по умолчанию
```shell
kubectl logs -l app=sprint2-task4 -n sprint2-task4
```

Сборка двух версий образа
```shell
docker build -t task4-booking-service:1.0 ./booking-service
```
```shell
docker build -t task4-booking-service:2.0 ./booking-service
```

Деплой приложения
```shell
helm install sprint2-task4 ./helm/booking-service --debug
```

```shell
helm upgrade --install sprint2-task4 ./helm/booking-service --debug
```

```shell
helm uninstall sprint2-task4 ./helm/booking-service --debug
```


поиск порта Istio
```shell
kubectl -n istio-system get svc istio-ingressgateway -o jsonpath='{range .spec.ports[?(@.name=="http2")]}{.nodePort}{end}'
# лучше
kubectl -n istio-system get svc istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}'
```

Обновление типа интерфеса у сервиса istio-ingressgateway c LoadBalancer на NodePort
```shell
kubectl -n istio-system patch svc istio-ingressgateway -p '{"spec": {"type": "NodePort"}}'
```
Проверить EXTERNAL-IP
```shell
kubectl -n istio-system get svc istio-ingressgateway
```

TODO Проверка конфигурации Envoy
```shell
istioctl proxy-config routes 'booking-service-v1-6f8f6d4c7b-hq2dx' -n sprint2-task4 --name http
#istioctl proxy-config routes 'booking-service-v2-7fd89bd5f7-ndj65' -n sprint2-task4 --name http -o json
```
booking-service-v1-6f8f6d4c7b-hq2dx   2/2     Running   0          3m8s
booking-service-v2-7fd89bd5f7-ndj65   2/2     Running   0          3m4s

