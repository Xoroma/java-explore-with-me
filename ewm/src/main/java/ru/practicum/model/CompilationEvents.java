package ru.practicum.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "compilations_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationEvents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "compilation")
    private Compilation compilation;
    @ManyToOne(optional = false)
    @JoinColumn(name = "event")
    private Event event;
}
