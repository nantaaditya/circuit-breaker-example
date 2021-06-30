package com.nantaaditya.circuitbreaker.listener;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * created by pramuditya.anantanur
 **/
@Slf4j
@Configuration
public class Resilience4jListener {
  @Bean
  public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer() {
    return new RegistryEventConsumer<CircuitBreaker>() {
      
      @Override
      public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
        entryAddedEvent.getAddedEntry().getEventPublisher()
            .onFailureRateExceeded(event -> log.error("circuit breaker {} failure rate {} on {}",
                event.getCircuitBreakerName(), event.getFailureRate(), event.getCreationTime())
            )
            .onSlowCallRateExceeded(event -> log.error("circuit breaker {} slow call rate {} on {}",
                event.getCircuitBreakerName(), event.getSlowCallRate(), event.getCreationTime())
            )
            .onCallNotPermitted(event -> log.error("circuit breaker {} call not permitted {}",
                event.getCircuitBreakerName(), event.getCreationTime())
            )
            .onError(event -> log.error("circuit breaker {} error with duration {}s",
                event.getCircuitBreakerName(), event.getElapsedDuration().getSeconds())
            )
            .onStateTransition(event -> log.warn("circuit breaker {} state transition from {} to {} on {}",
                event.getCircuitBreakerName(), event.getStateTransition().getFromState(),
                event.getStateTransition().getToState(), event.getCreationTime())
            );
      }

      @Override
      public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) { }

      @Override
      public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) { }
    };
  }

  @Bean
  public RegistryEventConsumer<TimeLimiter> timeLimiterEventConsumer() {
    return new RegistryEventConsumer<TimeLimiter>() {
      @Override
      public void onEntryAddedEvent(EntryAddedEvent<TimeLimiter> entryAddedEvent) {
        entryAddedEvent.getAddedEntry().getEventPublisher()
            .onTimeout(event -> log.error("time limiter {} timeout {} on {}",
                event.getTimeLimiterName(), event.getEventType(), event.getCreationTime())
            );
      }

      @Override
      public void onEntryRemovedEvent(EntryRemovedEvent<TimeLimiter> entryRemoveEvent) { }

      @Override
      public void onEntryReplacedEvent(EntryReplacedEvent<TimeLimiter> entryReplacedEvent) { }
    };
  }
}
