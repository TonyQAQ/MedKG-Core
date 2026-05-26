package com.sunsheen.hkks.common.uitl.mq;

import java.io.IOException;
//import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import net.sf.json.JSONObject;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sunsheen.jfids.system.config.Configs;

public class RabbitmqUtil {
	private final String EXCHANGE_NAME = "HKKS";
	private String routingKey = "label";
	private JSONObject connectInfor = JSONObject.fromObject(Configs.get("Rabbitmq.Infor"));
	private Boolean success = true;

	public RabbitmqUtil(String routingKey) {
		this.routingKey = routingKey;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public void sendMessage(Map<String, Object> param) {
		ConnectionFactory factory = new ConnectionFactory();
		try {
			factory.setHost(connectInfor.getString("host"));
			factory.setPort(connectInfor.getInt("port"));
			factory.setUsername(connectInfor.getString("username"));
			factory.setPassword(connectInfor.getString("password"));
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			// 声明交换机（名称和类型）
			channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
			// 消息发布（其中"directLogs"为交换机名称，"jay"为routingKey）
			channel.basicPublish(EXCHANGE_NAME, routingKey, null,
					JSONObject.fromObject(param).toString().getBytes());
			channel.close();
			connection.close();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		} catch (TimeoutException e) {
			success = false;
			e.printStackTrace();
		}
	}
}
