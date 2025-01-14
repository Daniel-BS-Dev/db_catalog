package com.daniel.DBCatalog.dbcatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daniel.DBCatalog.dbcatalog.dto.CategoryDTO;
import com.daniel.DBCatalog.dbcatalog.dto.RoleDTO;
import com.daniel.DBCatalog.dbcatalog.dto.UserDTO;
import com.daniel.DBCatalog.dbcatalog.dto.UserInsertDTO;
import com.daniel.DBCatalog.dbcatalog.dto.UserUpdateDTO;
import com.daniel.DBCatalog.dbcatalog.entities.Category;
import com.daniel.DBCatalog.dbcatalog.entities.Role;
import com.daniel.DBCatalog.dbcatalog.entities.User;
import com.daniel.DBCatalog.dbcatalog.repositories.RoleRepository;
import com.daniel.DBCatalog.dbcatalog.repositories.UserRepository;
import com.daniel.DBCatalog.dbcatalog.services.exceptions.DatabaseException;
import com.daniel.DBCatalog.dbcatalog.services.exceptions.ResourceNotFoundException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

@Service
public class UserService implements UserDetailsService {

	private static Logger logger = (Logger) LoggerFactory.getLogger(UserService.class);

	@Autowired
	private BCryptPasswordEncoder passwordEncoder; // metodo criado na minha class AppConfig

	@Autowired
	private UserRepository repository;

	@Autowired
	private RoleRepository roleRepository;

	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPage(PageRequest page, String name) {
		Page<User> list = repository.find(page, name);
		return list.map(x -> new UserDTO(x));
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> obj = repository.findById(id);// essa exceção esta sendo personalizada na class StandardError
		User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO insert(UserInsertDTO dto) {
		User entity = new User();
		copyEntity(entity, dto);
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity = repository.save(entity);
		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO updated(Long id, UserUpdateDTO dto) {
		try {
			User entity = repository.getById(id);
			copyEntity(entity, dto);
			entity = repository.save(entity);
			return new UserDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("id " + id + " does not exist");
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("id not found " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}

	private void copyEntity(User entity, UserDTO dto) {
		entity.setEmail(dto.getEmail());
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());

		entity.getRoles().clear();

		for (RoleDTO id : dto.getRoles()) {
			Role catId = roleRepository.getById(id.getId());
			entity.getRoles().add(catId);
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = repository.findByEmail(username);

		if (user == null) {
		   logger.error("user not found ", username);
		   throw new UsernameNotFoundException("Email not found");
		}
		  logger.info("User found " + username);
		  return user;
	    }
    }
