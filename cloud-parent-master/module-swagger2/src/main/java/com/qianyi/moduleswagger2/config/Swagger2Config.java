package com.qianyi.moduleswagger2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.HttpAuthenticationScheme;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
//@EnableSwagger2
@EnableOpenApi
public class Swagger2Config {

    @Value("${project.title:null}")
    String title;
    @Value("${project.swagger.enable:true}")
    private Boolean enable;

    @Bean
    public Docket createRestApi() {

        AuthorizationScope scope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] scopeArray = {scope};
        //存储令牌和作用域
        SecurityReference reference = new SecurityReference("authorization", scopeArray);
        List refList = new ArrayList();
        refList.add(reference);
        SecurityContext context = SecurityContext.builder().securityReferences(refList).build();

        List cxtList = new ArrayList();
        cxtList.add(context);

        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .enable(enable)
                .select()
                // 可以根据url路径设置哪些请求加入文档，忽略哪些请求
//                .paths(PathSelectors.any())
                .paths(PathSelectors.regex("(?!/error).+"))
                .paths(PathSelectors.regex("(?!/authentication).+"))
                .paths(PathSelectors.regex("(?!/authorization).+"))
                .paths(PathSelectors.regex("(?!/risk).+"))
                .build()
                .securitySchemes(Collections.singletonList(HttpAuthenticationScheme.
                        JWT_BEARER_BUILDER.name("authorization").build()))
                .securityContexts(Collections.singletonList(SecurityContext.builder()
                                .securityReferences(Collections.singletonList(SecurityReference.builder()
                                        .scopes(new AuthorizationScope[0])
                                        .reference("authorization")
                                        .build()))
                                .operationSelector(o -> o.requestMappingPattern().matches("/.*"))
                                .build()
                        )
                );

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(title + "开发接口文档") //设置文档的标题
                .description("1.返回状态码(code)为0，表示成功。-1,服务器异常。 1，登录已过期，请重新登录。2.授权失败。4.限定时间内超过请求次数 6.风险操作。7.未设置交易密码。8.帐号已在其他设备登录。其他代号，展示msg内容即可.<br>" +
                        "2.需要权限的接口，请求头加上authorization字段，值为服务器颁发的jwt令牌。令牌无感刷新，需实时更新") // 设置文档的描述
                .version("1.0.0") // 设置文档的版本信息-> 1.0.0 Version information
//                .termsOfServiceUrl("https://www.baidu.com") // 设置文档的License信息->1.3 License information
                .build();
    }
}
