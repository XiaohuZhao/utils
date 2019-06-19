package com.yorma.common.utils.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yorma.common.utils.object.ObjectUtil.isEmpty;

/**
 * @author zxh
 */
public class HttpClientHelper {
	public static String doGet(HttpGet request) throws IOException {
		return EntityUtils.toString(HttpClients.createDefault().execute(request).getEntity());
	}
	
	public static String doGet(String url) throws IOException {
		return doGet(new HttpGet(url));
	}
	
	public static String doPostForm(HttpPost request, Map<String, String> params) throws IOException {
		final List<BasicNameValuePair> parames = params.entrySet().stream().map(param -> new BasicNameValuePair(param.getKey(), param.getValue())).collect(Collectors.toList());
		request.setEntity(new UrlEncodedFormEntity(parames, "UTF-8"));
		final HttpEntity entity = HttpClients.createDefault().execute(request).getEntity();
		return EntityUtils.toString(entity);
	}
	
	public static String doPostForm(String url, Map<String, String> params) throws IOException {
		final List<BasicNameValuePair> parames = params.entrySet().stream().map(param -> new BasicNameValuePair(param.getKey(), param.getValue())).collect(Collectors.toList());
		return doPostForm(new HttpPost(url), params);
	}
	
	public static String doPostJson(HttpPost request, String jsonString) throws IOException {
		if (isEmpty(request.getHeaders(HTTP.CONTENT_TYPE))) {
			request.setHeader(HTTP.CONTENT_TYPE, "application/json");
		}
		if (isEmpty(request.getEntity())) {
			request.setEntity(new StringEntity(jsonString, ContentType.create("text/json", "UTF-8")));
		}
		return EntityUtils.toString(HttpClients.createDefault().execute(request).getEntity());
	}
	
	public static String doPostJson(String url, String jsonString) throws IOException {
		final HttpPost httpPost = new HttpPost(url);
		return doPostJson(httpPost, jsonString);
	}
}