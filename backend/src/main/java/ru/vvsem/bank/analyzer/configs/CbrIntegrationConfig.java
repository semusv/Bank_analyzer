package ru.vvsem.bank.analyzer.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.xml.transformer.UnmarshallingTransformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.vvsem.bank.analyzer.models.xml.ValCurs;
import ru.vvsem.bank.analyzer.models.xml.Valute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableIntegration
public class CbrIntegrationConfig {

    private static final String CBR_URL = "https://www.cbr.ru/scripts/XML_daily.asp";

    @Bean
    public MessageChannel exchangeRateChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel exchangeRateRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel exchangeRateProcessingChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel rateCheckChannel() {
        return new DirectChannel();
    }

    @Bean
    @InboundChannelAdapter(channel = "exchangeRateChannel",
            poller = @Poller(fixedDelay = "${cbr.poll.interval:3600000}"))
    public MessageSource<String> exchangeRateTrigger() {
        return () -> {
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return MessageBuilder.withPayload(date).build();
        };
    }

    @Bean
    public IntegrationFlow cbrExchangeRateFlow() {
        return IntegrationFlow
                .from("exchangeRateChannel")
                .enrichHeaders(h -> h
                        .headerExpression("cbrDate", "payload"))
                .handle(Http
                        .outboundGateway(CBR_URL + "?date_req={date}")
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(byte[].class)
                        .uriVariable("date", "headers['cbrDate']"))
                .transform(new UnmarshallingTransformer(jaxb2Marshaller()))
                .channel("exchangeRateProcessingChannel")
                .get();
    }


    @Bean
    public IntegrationFlow manualCbrFlow() {
        return IntegrationFlow
                .from("exchangeRateRequestChannel")
                .enrichHeaders(h -> h
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                        .headerExpression("cbrDate", "payload"))
                .handle(Http.outboundGateway(CBR_URL + "?date_req={date}")
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(byte[].class)
                        .uriVariable("date", "headers['cbrDate']"))
                .transform(new UnmarshallingTransformer(jaxb2Marshaller()))
                .channel("exchangeRateProcessingChannel")
                .get();
    }

    @Bean
    public IntegrationFlow processExchangeRatesFlow() {
        return IntegrationFlow
                .from("exchangeRateProcessingChannel")
                .handle("exchangeRateService", "processExchangeRates")
                .get();
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        System.out.println("ValCurs.class: " + ValCurs.class); // ← Добавь это
        System.out.println("Classloader: " + ValCurs.class.getClassLoader());
        System.out.println("Classpath: " + System.getProperty("java.class.path"));

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(ValCurs.class, Valute.class);
        return marshaller;
    }
}