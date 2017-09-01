# Optikal

Optikal is an optics library for Kotlin strongly inspired by [Scala Monocle](https://julien-truffaut.github.io/Monocle/).

## Motivation

> Optics are a group of purely functional abstractions to manipulate (get, set, modify, …) immutable objects.

Kotlin does a great job making java less verbose and concise. If you prefer "strong" types you probably prefer a `data class Token(val value: String)` instead of just a String.
But getters, setters don't compose, and when dealing with nested objects it could become cumbersome and extremely verbose hence Optikal.

Let's examine the following example. We have the following class structure. With the below defined Employee.

```
data class Street(val number: Int, val name: String)
data class Address(val city: String, val street: Street)
data class Company(val name: String, val address: Address)
data class Employee(val name: String, val company: Company)
```

```kotlin
import optikal.sample.*

val employee = Employee("John", Company("Awesome inc", Address("Awesome town", Street(23, "awesome street"))))
```

Due to some requirements we have to change all street names to be capitalize. Since we have the good practice of working with immutable objects, which kotlin favors, we cannot
simple mutate the name to be capitalized. Since we also care about performance we also don't want to create a new employee when we just need to change his address.

So we use the "convenient" copy method on data classes to give the employee a new company street name. Quite verbose... and in case we need to do something similar elsewhere we have
to construct the same construct.


```kotlin
employee.copy(
        company = employee.company.copy(
                address = employee.company.address.copy(
                        street = employee.company.address.street.copy(
                                name = employee.company.address.street.name.capitalize()
                        )
                )
        )
)
```
```kotlin
// Employee(name=John, company=Company(name=Awesome inc, address=Address(city=Awesome town, street=Street(number=23, name=Awesome street))))
```

So let's see how lenses can solve this problem!

## Lens

> A microscope lens is used to magnify and observe objects

Similar to a microscope lens, an optikal lens is used to observe what is in a object (product type). In the following example we have a `Token` type which
types a string token. We can create a lens for is which can then be used to see what is inside a Token object or to modify it.


```kotlin
import optikal.*

data class Token(val value: String)

val token = Token("1")

val tokenLens: Lens<Token, String> = Lens(
    get= { token: Token -> token.value },
    set= { value: String -> { token: Token -> token.copy(value= value) } }
)

tokenLens.modify({ "2" }, token)
```
```kotlin
// Token(value=2)
```

## Generating lenses

In the previous example we manually created a lens for a `Token`. Which is fine but verbose and boring, since we don't like boilerplate code we'd love
if this could just be generated for us.

If we add the `@Lenses` annotation to a data class its lenses will be automatically generated for it. So let's simplify our first example with lenses.
```
@Lenses data class Street(val number: Int, val name: String)
@Lenses data class Address(val city: String, val street: Street)
@Lenses data class Company(val name: String, val address: Address)
@Lenses data class Employee(val name: String, val company: Company)
```
```kotlin
import optikal.*
import optikal.sample.*

val employeeStreetNameLens: Lens<Employee, String> = employeeCompany() composeLens companyAddress() composeLens addressStreet() composeLens streetName()

employeeStreetNameLens.modify({ name -> name.capitalize() }, employee)
```
```kotlin
// Employee(name=John, company=Company(name=Awesome inc, address=Address(city=Awesome town, street=Street(number=23, name=Awesome street))))
```
## Getting started

```kotlin

"UPLOAD TO MAVEN AND ADD HERE"

```

# License

    Copyright (C) 2017 The Optikal Author

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

