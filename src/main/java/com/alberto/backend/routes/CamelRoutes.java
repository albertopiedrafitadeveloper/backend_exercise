package com.alberto.backend.routes;

import com.alberto.backend.model.Product;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
class CamelRoutes extends RouteBuilder {

  private static final String SIMILAR_PRODUCTS_ENDPOINT = "http://localhost:3001/product/${header.productId}/similarids?bridgeEndpoint=true";
  private static final String SINGLE_PRODUCT_ENDPOINT = "http://localhost:3001/product/${body}?bridgeEndpoint=true";

  @Override
  public void configure() {

    restConfiguration()
        .port("5000")
        .enableCORS(true)
        .apiContextPath("/api-doc")
        .apiProperty("api.title", "Test REST API")
        .apiProperty("api.version", "v1")
        .apiProperty("cors", "true")
        .component("servlet")
        .bindingMode(RestBindingMode.json)
        .dataFormatProperty("prettyPrint", "true");

    rest("/").description("Products REST service").id("api-route")
        .get("/product/{productId}/similar")
          .description("Returns the similar products to a given one ordered by similarity")
          .consumes(MediaType.APPLICATION_JSON)
          .produces(MediaType.APPLICATION_JSON)
          .route()
          .removeHeader(Exchange.HTTP_PATH)
          .removeHeader("CamelHttp*")
          .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
          .toD(SIMILAR_PRODUCTS_ENDPOINT)
          .unmarshal().json(JsonLibrary.Jackson, java.util.List.class)
          .removeHeader(Exchange.HTTP_PATH)
          .removeHeader("CamelHttp*")
          .split(body(), (oldExchange, newExchange) -> {
            List<Product> newBody = newExchange.getMessage().getBody(List.class);
            if(!Objects.isNull(oldExchange)) {
              newBody.add(oldExchange.getIn().getBody(Product.class));
            }
            newExchange.getIn().setBody(newBody);
            return newExchange;
          })
          .parallelProcessing()
          .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
          .toD(SINGLE_PRODUCT_ENDPOINT)
          .unmarshal(new JacksonDataFormat(Product.class))
        .end()
        .endRest();

  }

}
