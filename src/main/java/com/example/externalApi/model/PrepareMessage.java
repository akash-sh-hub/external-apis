package com.example.externalApi.model;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

@Repository("prepareMessage")
public class PrepareMessage {

	public ResponseEntity<String> getMessage(String requestData) {
		org.springframework.http.HttpHeaders responseHeaders = new org.springframework.http.HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");
		return new ResponseEntity<String>(requestData, responseHeaders, org.springframework.http.HttpStatus.OK);

	}

	public ResponseEntity<String> getMessageHindi(String requestData) {
		org.springframework.http.HttpHeaders responseHeaders = new org.springframework.http.HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");
		return new ResponseEntity<String>(requestData, responseHeaders, org.springframework.http.HttpStatus.OK);

	}
}
