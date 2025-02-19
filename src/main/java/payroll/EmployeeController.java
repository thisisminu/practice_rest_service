package payroll;

import java.util.List;
import java.util.stream.Collectors;


import org.springframework.http.ResponseEntity;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@RestController
class EmployeeController {

    private final EmployeeRepository repository;

    private final EmployeeModelAssembler assembler;

    EmployeeController(EmployeeRepository repository, EmployeeModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }


    // Aggregate root
    // tag::get-aggregate-root[]
//    @GetMapping("/employees")
//    List<Employee> all() {
//        return repository.findAll();
//    }
//    // end::get-aggregate-root[]


    // Every item  (RESTful version)
//    @GetMapping("/employees")
//    CollectionModel<EntityModel<Employee>> all() {
//
//        List<EntityModel<Employee>> employees = repository.findAll().stream()
//                .map(employee -> EntityModel.of(employee,
//                        linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
//                        linkTo(methodOn(EmployeeController.class).all()).withRel("employees")))
//                .collect(Collectors.toList());
//
//        return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
//    }


    // Every item (REST and using assembler to get every employee: evolved version)
    @GetMapping("/employees")
    CollectionModel<EntityModel<Employee>> all() {

        List<EntityModel<Employee>> employees = repository.findAll().stream() //
                .map(assembler::toModel) //
                .collect(Collectors.toList());

        return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }



//    @PostMapping("/employees")
//    Employee newEmployee(@RequestBody Employee newEmployee) {
//        return repository.save(newEmployee);
//    }

    // New version of POST that can handle the old and new clients (evolved version)
    @PostMapping("/employees")
    ResponseEntity<?> newEmployee(@RequestBody Employee newEmployee) {

        EntityModel<Employee> entityModel = assembler.toModel(repository.save(newEmployee));

        return ResponseEntity //
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
    }

    // Single item

//    @GetMapping("/employees/{id}")
//    Employee one(@PathVariable Long id) {
//
//        return repository.findById(id)
//                .orElseThrow(() -> new EmployeeNotFoundException(id));
//    }



    // Single item (RESTful version)
//    @GetMapping("/employees/{id}")
//    EntityModel<Employee> one(@PathVariable Long id) {
//
//        Employee employee = repository.findById(id) //
//                .orElseThrow(() -> new EmployeeNotFoundException(id));
//
//        return EntityModel.of(employee, //
//                linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(),
//                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
//    }


    // Single item (RESTful and using assembler to get a single employee: evolved version)
    @GetMapping("/employees/{id}")
    EntityModel<Employee> one(@PathVariable Long id) {

        Employee employee = repository.findById(id) //
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return assembler.toModel(employee);
    }




//    @PutMapping("/employees/{id}")
//    Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
//
//        return repository.findById(id)
//                .map(employee -> {
//                    employee.setName(newEmployee.getName());
//                    employee.setRole(newEmployee.getRole());
//                    return repository.save(employee);
//                })
//                .orElseGet(() -> {
//                    return repository.save(newEmployee);
//                });
//    }

    // new version of PUT to handle different clients (evolved version)
    @PutMapping("/employees/{id}")
    ResponseEntity<?> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {

        Employee updatedEmployee = repository.findById(id) //
                .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return repository.save(employee);
                }) //
                .orElseGet(() -> {
                    return repository.save(newEmployee);
                });

        EntityModel<Employee> entityModel = assembler.toModel(updatedEmployee);

        return ResponseEntity //
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
    }

//    @DeleteMapping("/employees/{id}")
//    void deleteEmployee(@PathVariable Long id) {
//        repository.deleteById(id);
//    }

    // new version of DELETE to handle different clients (evolved version)
    @DeleteMapping("/employees/{id}")
    ResponseEntity<?> deleteEmployee(@PathVariable Long id) {

        repository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
