package com.marcinmilewski.config

import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component
import org.springframework.validation.BindException

class GraphQLValidationException(errorMessage: String) : RuntimeException(errorMessage)

@Component
class GraphQLExceptionHandler : DataFetcherExceptionResolverAdapter() {

    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        return when (ex) {
            is BindException -> {
                GraphqlErrorBuilder.newError()
                    .message(ex.bindingResult.fieldErrors.map { it }.joinToString(separator = "\n"))
                    .errorType(ErrorType.InvalidSyntax)
                    .path(env.executionStepInfo.path)
                    .build()
            }

            is GraphQLValidationException -> {
                GraphqlErrorBuilder.newError()
                    .message(ex.message)
                    .errorType(ErrorType.InvalidSyntax)
                    .path(env.executionStepInfo.path)
                    .build()
            }

            else -> null
        }
    }
}
