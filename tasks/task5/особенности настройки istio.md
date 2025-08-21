В ходе решения задания столкнулся с проблемой: трафик к сервису booking-service для проверки canary deployment не распределялся в соответствии 90 и 10 процентов на версии deployment-v1 и deployment-v2 соответственно.
После долгого изучения проблемы выяснилось:
- по отношению к кластеру трафик может быть:
  - внешний, который приходит **не** от подов внутри кластера
  - внутренний, который приходит от подов внутри кластера
- решение для внутреннего трафика. 
  - Для реализации этого решения нужно сделать:
    - сервис [booking-ui](booking-ui), чтобы сделать внутренний трафик. Этот сервис перенаправляет внешние запросы на сервис booking-service тем самым создавая внутренний трафик.
    - сервис [booking-service](helm/booking-service)
    - [deployment-v1.yaml](helm/booking-service/templates/deployment-v1.yaml) первая версия приложения.
    - [deployment-v2.yaml](helm/booking-service/templates/deployment-v2.yaml) вторая версия приложения. По идеи можно использовать тот же образ приложения, но регулировать его поведение при помощи environment.
    - [service-v1.yaml](helm/booking-service/templates/service-v1.yaml)
    - [destination-rule.yaml](helm/booking-service/templates/destination-rule.yaml)
    - [virtual-service.yaml](helm/booking-service/templates/virtual-service.yaml)
      - текущий вариант содержит настройки для внешнего трафика
      - следующий блок настроек не нужен для этого решения(почему - объясняется в решении для внешнего трафика)
        ```yaml
          gateways:
            - booking
          http:
            - match:
                - uri:
                  prefix: "/ping"
        ```
  - чтобы определить URL на который надо:
    - определить IP и порт командой `minikube service booking-service --url`
    - к полученному результату добавить `/ping`
    - результат может быть таким `curl http://192.168.49.2:30167/ping`. Пример можно посмотреть в [check-canary.sh](check-canary.sh)
- решение для внешнего трафика. Промежуточный сервис [booking-ui](booking-ui) не нужен. 
  - Для реализации нужно сделать: 
    - сервис [booking-service](helm/booking-service)
    - [deployment-v1.yaml](helm/booking-service/templates/deployment-v1.yaml) первая версия приложения.
    - [deployment-v2.yaml](helm/booking-service/templates/deployment-v2.yaml) вторая версия приложения. По идеи можно использовать тот же образ приложения, но регулировать его поведение при помощи environment.
    - [service-v1.yaml](helm/booking-service/templates/service-v1.yaml)
    - [destination-rule.yaml](helm/booking-service/templates/destination-rule.yaml)
    - [virtual-service.yaml](helm/booking-service/templates/virtual-service.yaml)
    - добавляются
    - [gateway.yaml](helm/booking-service/templates/gateway.yaml)
  - Особенности реализации
    - создается Gateway, который пропускает через себя внешние запросы
    - в VirtualService добавляется ссылка на созданный Gateway
    - блок match в VirtualService важен для перенаправления запросов, т.к. из запроса вида `http://192.168.49.2:32323/ping` по префиксу будет определяться на какой VirtualService буден перенаправляться запрос. 
      ```yaml
      - match:
        - uri:
        prefix: "/ping"
      ```
    - блок hosts в VirtualService может иметь значения:
      - "*" В этом случае запрос вида `curl http://192.168.49.2:32323/ping` будет перенаправлен на нужный сервис
      - {{ .Chart.Name }}.default.svc.cluster.local В этом случае в заголовок запроса надо добавить поле Host и запрос получится вида `curl -H "Host: booking-service.default.svc.cluster.local" http://192.168.49.2:32323/ping`. Т.е. запрос должен иметь точно такой же заголовок Host, чтобы правила маршрутизации сработали.
    - чтобы определить URL на который надо направлять запрос к сервису нужно:
      - получить IP кластера `minikube ip`
      - получить порт, на который отправлять запрос `kubectl -n istio-system get svc istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}'`
      - окончательный результат команды может выглядеть так `curl -H "Host: booking-service.default.svc.cluster.local" http://192.168.49.2:32323/ping`
