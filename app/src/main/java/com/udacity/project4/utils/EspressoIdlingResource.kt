package com.udacity.project4.utils
import androidx.test.espresso.idling.CountingIdlingResource
//Позволяет следить за состоянием долго выполняющихся операций, чтобы тест не заканчивался
//прежде чем закончатся эти операции
object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    //Позволяет инкрементировать и декрементировать счетчик, который позволяет следить за выполнением операций
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
//позволяет избегать излишнего кода, ++ и -- счетчик при каждом запуске или прекращении операции
inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
    // Espresso does not work well with coroutines yet. See
    // https://github.com/Kotlin/kotlinx.coroutines/issues/982
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        function()
    } finally {
        EspressoIdlingResource.decrement() // Set app as idle.
    }
}