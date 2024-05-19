package com.zm.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;


/**
 * 自定义Swagger 接口文档的配置
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.zm.usercenter.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * api信息
     * @return
     */
    private ApiInfo apiInfo(){
        return new ApiInfo("用户中心",
                "用户中心接口文档",
                "1.0",
                "urn:tos",
                "zm",
                "Apache 2.0",
                "");
//        return new ApiInfoBuilder()
//                .title("用户中心")
//                .description("用户中心接口文档")
//                .termsOfServiceUrl("https://github.com/")
//                .contact(new Contact("zm", "https://github.com/", "xxx@qq.com"))
//                .version("1.0")
//                .build();
    }
}
