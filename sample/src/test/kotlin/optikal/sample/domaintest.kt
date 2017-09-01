package optikal.sample

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.forAll
import io.kotlintest.specs.StringSpec
import org.junit.runner.RunWith

//@RunWith(KTestJUnitRunner::class)
//class LensesTest : StringSpec() {
//
//    init {
//
//        val employee = LensExample.employee
//
//        "Modifying the name of a employees company street" {
//            forAll({ newStreet: String ->
//                val modifiedEmployee = LensExample.employeeStreetNameLens.modify({ newStreet }, employee)
//                modifiedEmployee.company.address.street.name == newStreet
//            })
//        }
//
//    }
//
//}