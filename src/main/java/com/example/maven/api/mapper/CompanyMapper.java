package com.example.maven.api.mapper;

import com.example.maven.api.dto.request.company.CompanyCreateDto;
import com.example.maven.api.dto.response.CompanyResponseDto;
import com.example.maven.persistence.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {
	public CompanyResponseDto toResponseDto(Company company){
		return new CompanyResponseDto(
				company.getId(),
				company.getName(),
				company.getCreatedAt()
		);
	}

	public Company fromCreateDto(CompanyCreateDto dto){
		Company company = new Company();
		company.setName(dto.name());
		return company;
	}
}
