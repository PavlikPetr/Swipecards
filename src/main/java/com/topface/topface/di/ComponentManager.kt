package com.topface.topface.di

/**
 * Менеджер компонентов, помогает создавать новые и получить уже созданные без особой боли
 * Created by tiberal on 07.02.17.
 */
object ComponentManager {

    val componentsMap = mutableMapOf<Class<*>, Any?>()
    var lastImmortalComponentClass: Class<*>? = null

    @Suppress("UNCHECKED_CAST")
    @JvmOverloads inline fun <T : Any?> obtainComponent(componentClass: Class<T>, terminateLastImmortal: Boolean = false,
                                                        factory: () -> T): T =
            (componentsMap[componentClass] ?: factory().apply {
                if (terminateLastImmortal && this is Immortal) {
                    /**
                     * Должен остаться только один
                     * terminateLastImmortal говорит, что нужно удалить предыдущий Immortal
                     * компонент, при создании нового. Актуально для экранов на которых одна ViewModel
                     * Для фидов не пойдет
                     */
                    lastImmortalComponentClass?.let { releaseComponent(it) }
                    lastImmortalComponentClass = componentClass
                }
                componentsMap.put(componentClass, this)
            }) as T

    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> obtainComponent(componentClass: Class<T>): T {
        val component = componentsMap[componentClass]
        if (component != null) {
            return component as T
        } else {
            throw ComponentNotFound()
        }
    }

    fun releaseComponent(clazz: Class<*>) = componentsMap.remove(clazz)
}