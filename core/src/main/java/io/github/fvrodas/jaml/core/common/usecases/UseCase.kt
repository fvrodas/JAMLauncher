package io.github.fvrodas.jaml.core.common.usecases

abstract class UseCase<Type, Param> {
    abstract suspend operator fun invoke(params: Param): Type
}
