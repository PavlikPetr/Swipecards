package com.topface.topface.ui.dialogs.trial_vip_experiment.base

/**
 * Интерфейс фабрики. Что-нибудь конструирует на основе типа эксперимента.
 * Created by tiberal on 16.11.16.
 */
interface IBoilerplateFactory<out T> {
    fun construct(@ExperimentsType.ExperimentsType type: Long): T
}