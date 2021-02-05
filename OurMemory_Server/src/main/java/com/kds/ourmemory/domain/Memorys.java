package com.kds.ourmemory.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Memorys {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "memory_id")
	private Long id;
	
	@Column(nullable = false, name="memory_name")
	private String name;
	
	@Column(nullable = true, name="memory_contents")
	private String contents;
	
	@Column(nullable = true, name="memory_start_date")
	private Date startDate;
	
	@Column(nullable = true, name="memory_end_date")
	private Date endDate;
	
	@Column(nullable = false, name="reg_date")
	private Date regDate;
	
	@Column(nullable = false, name="memory_used")
	private boolean used;
}
