package eu.getsoftware.hotelico.main.hotel.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = { "eu.getsoftware.hotelico.infrastructure.hotel.repository" })
@EntityScan(basePackages = {"eu.getsoftware.hotelico.infrastructure.model"})
public class InfrastructureConfiguration
{
	@Bean
	BeanFactoryPostProcessor beanFactoryPostProcessor(ApplicationContext beanRegistry) {
		return beanFactory -> {
			genericApplicationContext((BeanDefinitionRegistry) ((AnnotationConfigServletWebServerApplicationContext) beanRegistry).getBeanFactory());
		};
	}
	
	void genericApplicationContext(BeanDefinitionRegistry beanRegistry) {
		ClassPathBeanDefinitionScanner beanDefinitionScanner = new ClassPathBeanDefinitionScanner(beanRegistry);
		beanDefinitionScanner.addIncludeFilter(removeModelAndEntitiesFilter());
		beanDefinitionScanner.scan("eu.getsoftware.hotelico");
	}
	
	static TypeFilter removeModelAndEntitiesFilter() {
		return (MetadataReader mr, MetadataReaderFactory mrf) -> !mr.getClassMetadata()
				.getClassName()
				.endsWith("Model");
	}
}
