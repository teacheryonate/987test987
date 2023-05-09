package com.example.userservice.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;

@RestController
@RequestMapping("/")//이 서비스 호출할 때, http://localhost/user-service/welcome 형식으로 호출하지만, 클래스단의 uri는 없어도 됨. apigateway-service에서 안 써도 되게 필터 적용했음.
public class UserController {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private Greeting greeting;
	
	@Value("${greeting.message}")
	private String msg;
	
	@Autowired
	private UserService userService;
	
	
	
	
	
	@GetMapping("/health_check")
	public String status() {
		return String.format("It's working in user service "
				+", port(local.server.port)="+env.getProperty("local.server.port")
				+", port(server.port)="+env.getProperty("server.port")
				+", token secret="+env.getProperty("token.secret")
				+", token expiration time="+env.getProperty("token.expiration_time")
				);
	}

	
	@GetMapping("/welcome")
	public String welcome() {
		//return env.getProperty("greeting.message");
		return "유저, 환영합니다. "+greeting.getMessage()+" : "+msg;
	}
	
	@PostMapping("/users")
	public ResponseEntity<?> createUser(@RequestBody RequestUser user) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserDto userDto = mapper.map(user, UserDto.class);
		userService.createUser(userDto);
		
		ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
	}
	
	@GetMapping("/users")
	public ResponseEntity<List<ResponseUser>> getUsers(){
		Iterable<UserEntity> userList = userService.getUserByAll();
		List<ResponseUser> result = new ArrayList<>();
		
		userList.forEach(v-> {
			result.add(new ModelMapper().map(v, ResponseUser.class));
		});
		
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping("/users/{userId}")
	public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId){
		UserDto userDto = userService.getUserByUserId(userId);
		
			ResponseUser retrunValue = new ModelMapper().map(userDto, ResponseUser.class);
		
		return ResponseEntity.status(HttpStatus.OK).body(retrunValue);
	}
	
}
