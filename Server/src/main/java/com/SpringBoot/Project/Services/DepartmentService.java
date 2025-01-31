package com.SpringBoot.Project.Services;

import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Repositories.DepartmentInterface;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentInterface departmentInterface;

    public DepartmentService(DepartmentInterface departmentInterface) {
        this.departmentInterface = departmentInterface;
    }

    public Result<List<Department>> getAllDepartments(){
        List<Department> departments = departmentInterface.findAll();
        return Result.success(departments, "Departments fetched successfully.");
    }

    public Result<Department> getDepartmentById(long id){
        Optional<Department> department = departmentInterface.findById(id);
        if(department.isPresent()){
            return Result.success(department.get(), "Department fetched successfully.");
        }else{
            return Result.failure("Department not found.", List.of("No departments found with id of " + id));
        }
    }

    public Result<Department> saveOrUpdateDepartment(Department department){
        try{
            Department savedDepartment = departmentInterface.save(department);
            return Result.success(savedDepartment, "Department has been saved.");
        }catch (Exception e){
            return Result.failure("Department could not be saved.", List.of(e.getMessage()));
        }
    }

    public Result<Void> deleteDepartmentById(long id){
        if(departmentInterface.existsById(id)){
            departmentInterface.deleteById(id);
            return Result.success(null, "Department was deleted");
        }else{
            return Result.failure("Department not found", List.of("No departments found with id of" + id));
        }
    }
}
