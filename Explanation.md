

`OrderService.processOrder` валидирует заказ, получает результат расчёта, передаёт его на печать и сохраняет заказ. Порядок действий совпадает с исходным.

- Strategy. Правила расчёта скидки и комиссии реализуют интерфейс `PricingStrategy` с методом `calculate(Order)`. При обработке заказа нужное правило выбирается из `Map` по типу клиента или способу оплаты, а комиссия рассчитывается от полной суммы заказа.
- Factory Method. Метод `OrderPricingFactory.create()` собирает калькулятор `StrategyOrderPricing` и передаёт ему две стратегии через `createDiscountStrategy()` и `createCommissionStrategy()`. В `StandardOrderPricingFactory` эти методы возвращают стандартные стратегии, а другой подкласс может переопределить любой из них.

## SOLID

1. S. В исходном `processOrder` проверки заказа, расчёт, строки чека и добавление в список стояли в одном методе. Теперь проверками занимается `OrderValidator`, расчётом — `StrategyOrderPricing`, печатью — `ConsoleReceiptPrinter`, а сохранением — `InMemoryOrderRepository`. В `processOrder` остался порядок вызовов.

2. O. Чтобы добавить новый тип клиента, в исходнике пришлось бы дописать `else if` прямо в `processOrder`. Теперь, например, скидку для `EMPLOYEE` можно добавить в подклассе `StandardOrderPricingFactory`, переопределив `createDiscountStrategy()` и передав остальные типы стандартной стратегии.

3. L. В исходнике скидки `REGULAR` и `VIP` были отдельными ветками, встроенными в расчёт заказа. Теперь обе стратегии принимают `Order` и возвращают сумму скидки по одному контракту `PricingStrategy.calculate(Order)`. Для заказа на 1000 это 0 и 50 соответственно, и `StrategyOrderPricing` может использовать любую из них через один и тот же вызов.

4. I. В исходнике печать чека была частью `processOrder` вместе с расчётом и сохранением. Теперь `ConsoleReceiptPrinter` реализует только `ReceiptPrinter.print()`, а для сохранения есть отдельный интерфейс `OrderRepository` с методом `save()`.

5. D. В исходнике `OrderService` напрямую вызывал `System.out.println()` и добавлял заказ в свой `ArrayList`. Теперь в конструктор можно передать другой `ReceiptPrinter` или `OrderRepository`, например репозиторий с сохранением в файл, и `processOrder` вызовет тот же метод `save()`.