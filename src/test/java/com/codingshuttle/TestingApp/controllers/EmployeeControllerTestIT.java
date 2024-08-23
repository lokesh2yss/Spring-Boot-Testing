package com.codingshuttle.TestingApp.controllers;

import com.codingshuttle.TestingApp.TestContainerConfiguration;
import com.codingshuttle.TestingApp.dto.EmployeeDto;
import com.codingshuttle.TestingApp.entities.Employee;
import com.codingshuttle.TestingApp.repositories.EmployeeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;


class EmployeeControllerTestIT extends AbstractIntegrationTest{

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void testGetEmployeeById_success() {
        Employee savedEmployee = employeeRepository.save(testEmployee);

        webTestClient.get()
                .uri("/employees/{id}", savedEmployee.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(EmployeeDto.class)
                //.isEqualTo(testEmployeeDto);
                .value(employeeDto -> {
                    assertThat(employeeDto.getEmail()).isEqualTo(savedEmployee.getEmail());
                    assertThat(employeeDto.getId()).isEqualTo(savedEmployee.getId());
                });
    }

    @Test
    void testGetEmployeeById_failure() {
        webTestClient.get()
                .uri("/employees/1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateNewEmployee_whenEmployeeAlreadyExists_ThenThrowException() {
        Employee savedEmployee = employeeRepository.save(testEmployee);

        webTestClient.post()
                .uri("/employees")
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    void testCreateNewEmployee_whenEmployeeDoesNotExist_thenCreateEmployee() {
        webTestClient.post()
                .uri("employees")
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo(testEmployeeDto.getEmail())
                .jsonPath("$.name").isEqualTo(testEmployeeDto.getName());
    }

    @Test
    void testUpdateEmployee_whenEmployeeDoesNotExist_thenThrowAnException() {
        webTestClient.put()
                .uri("/employees/999")
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testUpdateEmployee_whenAttemptingToUpdateEmail_thenThrowException() {
        Employee savedEmployee = employeeRepository.save(testEmployee);

        testEmployeeDto.setName("Random Name");
        testEmployeeDto.setEmail("random@gmail.com");
        webTestClient.put()
                .uri("/employees/{id}", savedEmployee.getId())
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    void testUpdateEmployee_whenValidEmployeeI_thenUpdateEmployee() {
        Employee savedEmployee = employeeRepository.save(testEmployee);
        testEmployeeDto.setName("Random Name");
        testEmployeeDto.setSalary(199L);
        testEmployeeDto.setId(savedEmployee.getId());
        webTestClient.put()
                .uri("/employees/{id}", savedEmployee.getId())
                .bodyValue(testEmployeeDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(EmployeeDto.class)
                .isEqualTo(testEmployeeDto);
                //.jsonPath("$.name").isEqualTo(testEmployeeDto.getName())
                //.jsonPath("$.email").isEqualTo(testEmployeeDto.getEmail());
    }

    @Test
    void testDeleteEmployee_whenEmployeeDoesNotExist_thenThrowException() {
        webTestClient.delete()
                .uri("/employees/1")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testDeleteEmployee_whenValidEmployeeExists_thenDeleteEmployee() {
        Employee savedEmployee = employeeRepository.save(testEmployee);
        webTestClient.delete()
                .uri("employees/{id}", savedEmployee.getId())
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);

        webTestClient.delete()
                .uri("employees/{id}", savedEmployee.getId())
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}