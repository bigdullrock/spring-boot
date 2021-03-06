/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.jms;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.Assert;

/**
 * Configure {@link JmsListenerContainerFactory} with sensible defaults.
 *
 * @author Stephane Nicoll
 * @since 1.3.3
 */
public final class JmsListenerContainerFactoryConfigurer {

	private DestinationResolver destinationResolver;

	private JtaTransactionManager transactionManager;

	private JmsProperties jmsProperties;

	/**
	 * Set the {@link DestinationResolver} to use or {@code null} if no destination
	 * resolver should be associated with the factory by default.
	 * @param destinationResolver the {@link DestinationResolver}
	 */
	@Autowired(required = false)
	public void setDestinationResolver(DestinationResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	/**
	 * Set the {@link JtaTransactionManager} to use or {@code null} if the JTA support
	 * should not be used.
	 * @param transactionManager the {@link JtaTransactionManager}
	 */
	@Autowired(required = false)
	public void setTransactionManager(JtaTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Set the {@link JmsProperties to use}.
	 * @param jmsProperties the {@link JmsProperties}
	 */
	@Autowired
	public void setJmsProperties(JmsProperties jmsProperties) {
		this.jmsProperties = jmsProperties;
	}

	/**
	 * Create a new and pre-configured {@link DefaultJmsListenerContainerFactory} instance
	 * for the specified {@link ConnectionFactory}.
	 * @param connectionFactory the {@link ConnectionFactory} to use.
	 * @return a pre-configured {@link DefaultJmsListenerContainerFactory}
	 */
	public DefaultJmsListenerContainerFactory createJmsListenerContainerFactory(
			ConnectionFactory connectionFactory) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configure(factory, connectionFactory);
		return factory;
	}

	/**
	 * Apply the default settings for the specified jms listener container factory. The
	 * factory can be further tuned and default settings can be overridden.
	 * @param factory the {@link DefaultJmsListenerContainerFactory} instance to configure
	 * @param connectionFactory the {@link ConnectionFactory} to use
	 */
	public void configure(DefaultJmsListenerContainerFactory factory,
			ConnectionFactory connectionFactory) {
		Assert.notNull(factory, "Factory must not be null");
		Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
		factory.setConnectionFactory(connectionFactory);
		factory.setPubSubDomain(this.jmsProperties.isPubSubDomain());
		if (this.transactionManager != null) {
			factory.setTransactionManager(this.transactionManager);
		}
		else {
			factory.setSessionTransacted(true);
		}
		if (this.destinationResolver != null) {
			factory.setDestinationResolver(this.destinationResolver);
		}
		JmsProperties.Listener listener = this.jmsProperties.getListener();
		factory.setAutoStartup(listener.isAutoStartup());
		if (listener.getAcknowledgeMode() != null) {
			factory.setSessionAcknowledgeMode(listener.getAcknowledgeMode().getMode());
		}
		String concurrency = listener.formatConcurrency();
		if (concurrency != null) {
			factory.setConcurrency(concurrency);
		}
	}

}
