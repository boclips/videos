package com.boclips.api.presentation.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.hateoas.core.EvoInflectorRelProvider
import org.springframework.hateoas.hal.CurieProvider
import org.springframework.hateoas.hal.HalConfiguration
import org.springframework.hateoas.hal.Jackson2HalModule

@Configuration
class HateoasConfig {

    @Bean
    fun halObjectMapperConfigurer(): HalObjectMapperConfigurer {
        return HalObjectMapperConfigurer()
    }

    class HalObjectMapperConfigurer : BeanPostProcessor, BeanFactoryAware {

        private lateinit var beanFactory: BeanFactory

        override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
            if (bean is ObjectMapper) {
                postProcessHalObjectMapper(bean)
            }
            return bean
        }

        private fun postProcessHalObjectMapper(objectMapper: ObjectMapper) {
            val curieProvider = getCurieProvider(beanFactory)

            objectMapper.registerModule(Jackson2HalModule())
            val halConfiguration = HalConfiguration().withRenderSingleLinks(HalConfiguration.RenderSingleLinks.AS_SINGLE)
            objectMapper.setHandlerInstantiator(Jackson2HalModule.HalHandlerInstantiator(
                    EvoInflectorRelProvider(),
                    curieProvider,
                    linkRelationMessageSource(),
                    halConfiguration))
        }

        private fun getCurieProvider(factory: BeanFactory) = try {
            factory.getBean(CurieProvider::class.java)
        } catch (e: NoSuchBeanDefinitionException) {
            null
        }

        override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
            return bean
        }

        override fun setBeanFactory(beanFactory: BeanFactory) {
            this.beanFactory = beanFactory
        }

        fun linkRelationMessageSource(): MessageSourceAccessor {

            try {

                val messageSource = ReloadableResourceBundleMessageSource()
                messageSource.setBasename("classpath:rest-messages")

                return MessageSourceAccessor(messageSource)

            } catch (o_O: Exception) {
                throw BeanCreationException("resourceDescriptionMessageSourceAccessor", "", o_O)
            }

        }

    }
}