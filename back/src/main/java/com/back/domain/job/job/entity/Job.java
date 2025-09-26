package com.back.domain.job.job.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "job")
@Getter @Setter
@NoArgsConstructor
public class Job extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobAlias> aliases;

    public Job(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addAlias(JobAlias alias) {
        aliases.add(alias);
        alias.setJob(this);
    }
}
