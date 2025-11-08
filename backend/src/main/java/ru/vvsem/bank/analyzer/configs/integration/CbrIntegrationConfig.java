package ru.vvsem.bank.analyzer.configs.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateService;
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableIntegration
@ConditionalOnProperty(name = "cbr.integration.enabled", havingValue = "true", matchIfMissing = true)
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
            poller = @Poller(fixedDelay = "${cbr.integration.poll.interval:3600000}"))
    public MessageSource<String> exchangeRateTrigger() {
        return () -> {
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return MessageBuilder.withPayload(date).build();
        };
    }

    @Bean
    public IntegrationFlow cbrExchangeRateFlow(ExchangeRateServiceImpl exchangeRateServiceImpl) {
        return IntegrationFlow
                .from("exchangeRateChannel")
                .enrichHeaders(h -> h
                        .headerExpression("cbrDate", "payload"))
                // Преобразуем строку "dd/MM/yyyy" в LocalDate
                .transform(payload -> LocalDate.parse((String) payload, DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                // Проверяем, есть ли уже данные в БД
                .filter(exchangeRateServiceImpl, "needLoadForDate", spec -> spec
                        .discardChannel("rateCheckChannel"))
                // Обратно в строку для использования в URL
                .transform(LocalDate.class,
                        date -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                // Теперь делаем HTTP-запрос
                .handle(Http
                        .outboundGateway(CBR_URL + "?date_req={date}")
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(byte[].class)
                        .uriVariable("date", "payload"))
                .transform(new UnmarshallingTransformer(jaxb2Marshaller()))
                .channel("exchangeRateProcessingChannel")
                .get();
    }

    @Bean
    public IntegrationFlow manualCbrFlow(ExchangeRateServiceImpl cacheService) {
        return IntegrationFlow
                .from("exchangeRateRequestChannel")
                .enrichHeaders(h -> h
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE))
                .transform(payload -> LocalDate.parse((String) payload, DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .filter(cacheService, "needLoadForDate")
                .transform(LocalDate.class, date -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .handle(Http.outboundGateway(CBR_URL + "?date_req={date}")
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(byte[].class)
                        .uriVariable("date", "payload"))
                .transform(new UnmarshallingTransformer(jaxb2Marshaller()))
                .channel("exchangeRateProcessingChannel")
                .get();
    }

    @Bean
    public IntegrationFlow rateCheckDiscardFlow() {
        return IntegrationFlow.from("rateCheckChannel")
                .handle(message ->
                        System.out.println("Пропуск: данные уже существуют для даты " + message.getPayload()))
                .get();
    }

    @Bean
    public IntegrationFlow processExchangeRatesFlow(ExchangeRateService exchangeRateService) {
        return IntegrationFlow
                .from("exchangeRateProcessingChannel")
                .handle(exchangeRateService, "processExchangeRates")
                .get();
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(ValCurs.class, Valute.class);
        return marshaller;
    }
}