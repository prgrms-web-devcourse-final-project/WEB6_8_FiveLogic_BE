package com.back.domain.roadmap.task.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task")
@Getter
@NoArgsConstructor
public class Task extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<TaskAlias> aliases = new ArrayList<>();

    public Task(String name) {
        this.name = name;
        this.aliases = new ArrayList<>();
    }

    public void addAlias(TaskAlias alias) {
        if (aliases == null) {
            aliases = new ArrayList<>();
        }
        aliases.add(alias);
        alias.linkToTask(this);
    }
}
