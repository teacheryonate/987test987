package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;

@Service
public class UserServiceImpl implements UserService {
	
	UserRepository userRepository;
	
	
	BCryptPasswordEncoder passwordEncoder;
	
	Environment env;
	
	RestTemplate restTemplate;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, 
			BCryptPasswordEncoder passwordEncoder, Environment env, 
			RestTemplate restTemplate) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.env = env;
		this.restTemplate = restTemplate;
	}
	

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if(userEntity == null) {
			throw new UsernameNotFoundException(email);
		}
		
		return new ModelMapper().map(userEntity, UserDto.class);
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("::::::::::::::::UserServiceImpl:::::sloadUserByUsername(String username):::::::::::::::::::::");
		UserEntity userEntity = userRepository.findByEmail(username);
		
		if(userEntity == null) {
			throw new UsernameNotFoundException(username);
		}
		
		return new User(userEntity.getEmail(), userEntity.getEncryptPwd(), true, true, true, true, new ArrayList<>());
	}
	
	




	@Override
	public UserDto createUser(UserDto userDto) {
		
		userDto.setUserId(UUID.randomUUID().toString());
		
		ModelMapper mapper = new ModelMapper();;
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = mapper.map(userDto, UserEntity.class);
		userEntity.setEncryptPwd(passwordEncoder.encode(userDto.getPwd()));
		
		userRepository.save(userEntity);
		
		UserDto returnUserDto = mapper.map(userEntity, UserDto.class);
		
		return returnUserDto;
	}



	@Override
	public UserDto getUserByUserId(String userId) {
		// TODO Auto-generated method stub
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if(userEntity == null) {
			throw new UsernameNotFoundException("user not found");
		}
		
		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
		
		String orderUrl = String.format(env.getProperty("order_service.url"), userId);
		System.out.println(orderUrl+":::::::::::::::::::::");
		ResponseEntity<List<ResponseOrder>> orderListResponse =
				restTemplate.exchange(orderUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ResponseOrder>>() {
				});
		
		
		List<ResponseOrder> orderList =orderListResponse.getBody();
		
		System.out.println(orderList+":::::::::::::::::::::");
		userDto.setOrders(orderList);
		
		return userDto;
	}



	@Override
	public Iterable<UserEntity> getUserByAll() {
		// TODO Auto-generated method stub
		return userRepository.findAll();
	}





}
