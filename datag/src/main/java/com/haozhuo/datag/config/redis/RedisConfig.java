package com.haozhuo.datag.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

/**
 * Spring boot配置多个redis数据库。
 * 我这里使用的是Redis Sentinel模式.数据库的地址都相同.只不过一个Redis数据库是0(默认),另一个是1
 * 看了官网的介绍：
 * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-connecting-to-redis
 * 说可以通过实现LettuceClientConfigurationBuilderCustomizer或JedisClientConfigurationBuilderCustomizer来配置多个
 * redis数据库，但是翻遍了整个网络都没找到具体实现的examples.
 * 只能使用以下这种旧办法：
 * http://www.cnblogs.com/lchb/articles/7222870.html
 */
@Configuration
class RedisConfig {

    @Value("${spring.redis.sentinel.master}")
    private String sentinelMaster;

    @Value("${spring.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Value("${spring.redis.pool.max-active:8}")
    private int maxTotal;

    @Value("${spring.redis.pool.min-idle:0}")
    private int minIdle;

    @Value("${spring.redis.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.redis.pool.max-wait:-1}")
    private long maxWaitMillis;

    @Value("${spring.redis.password:}")
    private String password;

    //连接超时时间(毫秒)
    @Value("${spring.redis.timeout:3600}")
    private int timeout;

    private RedisSentinelConfiguration redisSentinelConfiguration() {
        Set<String> setNodes = stream(sentinelNodes.split(",")).map(String::trim).collect(toSet());
        return new RedisSentinelConfiguration(sentinelMaster, setNodes);
    }

    private JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        return poolConfig;
    }

    private StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }

    @SuppressWarnings("deprecation")
    private JedisConnectionFactory buildConnectionFactory(int database) {
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisSentinelConfiguration(), jedisPoolConfig());
        connectionFactory.setUsePool(true);
        connectionFactory.setTimeout(timeout);
        connectionFactory.setDatabase(database);
        connectionFactory.setPassword(password);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    private StringRedisTemplate buildRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setValueSerializer(stringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "redisTemplate1")
    public StringRedisTemplate redisTemplate1() {
        return buildRedisTemplate(buildConnectionFactory(1));
    }

    @Bean(name = "redisTemplate0")
    @Primary
    public StringRedisTemplate redisTemplate0() {
        return buildRedisTemplate(buildConnectionFactory(0));
    }
}
