package com.back.domain.job.job.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job")
@Getter
@NoArgsConstructor
public class Job extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobAlias> aliases = new ArrayList<>();

    public Job(String name, String description) {
        this.name = name;
        this.description = description;
        this.aliases = new ArrayList<>();
    }

    public void addAlias(JobAlias alias) {
        if (aliases == null) {
            aliases = new ArrayList<>();
        }
        aliases.add(alias);
        alias.linkToJob(this);
    }
}
