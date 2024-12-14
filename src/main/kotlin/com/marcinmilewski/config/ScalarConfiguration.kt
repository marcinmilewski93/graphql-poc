package com.marcinmilewski.config

import graphql.scalars.ExtendedScalars
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer

@Configuration
class ScalarConfiguration {

    @Bean
    fun scalarConfig() = RuntimeWiringConfigurer { it.scalar(ExtendedScalars.DateTime).scalar(ExtendedScalars.Object) }
}
