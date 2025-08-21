1. Деплой сервиса с двумя версиями в виде helm-chart. Т.к. поведение сервиса регулируется через настройки environment используется один [values.yaml](../helm/booking-service/values.yaml)
2. Установка Istio
3. изменен скрипт check-canary.sh
4. написал инструкцию для настройки canary deployment [особенности настройки istio.md](../%D0%BE%D1%81%D0%BE%D0%B1%D0%B5%D0%BD%D0%BD%D0%BE%D1%81%D1%82%D0%B8%20%D0%BD%D0%B0%D1%81%D1%82%D1%80%D0%BE%D0%B9%D0%BA%D0%B8%20istio.md)