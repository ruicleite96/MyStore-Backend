package mystore;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import mystore.models.MyStore;
import mystore.security.JwtFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class MyStoreBackendApplication {

    @Bean
    public FilterRegistrationBean simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://mystore.blurryface.pt", "http://blurryface.pt"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setExposedHeaders(Collections.singletonList("Access-Token"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean getJwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns(
                "/clientes/*",
                "/utilizadores/editarDados",
                "/utilizadores/dados",
                "/utilizadores/alterarPassword",
                "/utilizadores/clientes",
                "/utilizadores/funcionarios",
                "/utilizadores/funcionarios/*",
                "/utilizadores/clientes/*",
                "/produtos/maisVendidosDetail/*",
                "/produtos/criar",
                "/produtos/apagar",
                "/produtos/editar",
                "/encomendas/*",
                "/categorias/apagar",
                "/categorias/criar",
                "/promocoes/*"
        );
        return registrationBean;
    }

    @Bean
    public MyStore getMyStore() {
        return new MyStore();
    }

    @Bean
    public Module getHibernate5Module() {
        return new Hibernate5Module();
    }

    public static void main(String[] args) {
        SpringApplication.run(MyStoreBackendApplication.class, args);
    }
}
