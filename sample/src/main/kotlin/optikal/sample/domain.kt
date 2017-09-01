package optikal.sample

import optikal.*

@Lenses data class Street(val number: Int, val name: String)
@Lenses data class Address(val city: String, val street: Street)
@Lenses data class Company(val name: String, val address: Address)
@Lenses data class Employee(val name: String, val company: Company)

object LensExample {

    val employee = Employee("John", Company("Awesome inc", Address("Awesome town", Street(23, "awesome street"))))

    val employee1 = employee.copy(
            company = employee.company.copy(
                    address = employee.company.address.copy(
                            street = employee.company.address.street.copy(
                                    name = employee.company.address.street.name.capitalize()
                            )
                    )
            )
    )

    val employeeStreetNameLens: Lens<Employee, String> = employeeCompany() composeLens companyAddress() composeLens addressStreet() composeLens streetName()

    val employee2 = employeeStreetNameLens.modify({ name -> name.capitalize() }, employee)

    fun main(args: Array<String>) {
        println(employee)

        println("Is running nested copies equal to composing lenses? ${employee1 == employee2}.")
        println("The result is $employee2")
    }

}
