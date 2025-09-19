package com.back.domain.roadmap.task.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "task")
@Getter @Setter
@NoArgsConstructor
public class Task extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<TaskAlias> aliases;

    public Task(String name) {
        this.name = name;
    }

    public void addAlias(TaskAlias alias) {
        aliases.add(alias);
        alias.setTask(this);
    }
}
