# Курс основ программирования на МКН СПбГУ
## Проект 2: key-value база данных

[Постановка задачи](./TASK.md)

### Документация

Данная утилита является консольным интерфейсом к базе данных, которая поддерживает операции по поиску, вставке и удалению текстовых значений по соответствующим им текстовым ключам.

#### Входные данные

Данная утилита работает в консольном режиме и принимает входные данные через параметры командной строки.

#### Интерфейс:

Есть три вида команд:
* *add key value* - добавляет в базу данных значение *value* по ключу *key*, если в базе ещё нет такого ключа, иначе ничего не делает и выводит сообщение пользователю
* *remove key* - если в базе есть ключ *key*, то удаляет его, иначе ничего не делает и выводит сообщение пользователю
* *get key* - если в базе есть ключ *key*, то выводит значение ему соответствующее, иначе ничего не делает и выводит сообщение пользователю

Пример использования:

    $ db add key value
    $ db get key
    value
