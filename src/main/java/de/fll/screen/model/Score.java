package de.fll.screen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "score")
public final class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "id")
    private long id;

    @Column(name = "points", nullable = false)
    private double points;

    @Column(name = "time", nullable = false)
    private int time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Score(double points, int time) {
        this.points = points;
        this.time = time;
    }

    public Score() {
        // empty constructor for jpa
        this(-1, -1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Score score)) {
            return false;
        }
        return Double.compare(points, score.points) == 0 && time == score.time;
    }

    @Override
    public String toString() {
        if (points == -1) {
            return "---";
        }
        if (time == -1) {
            return points + "";
        }
        return points + "(" + time + ')';
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, time);
    }

    public long getId() {
        return id;
    }

    public double getPoints() {
        return points;
    }

    public int getTime() {
        return time;
    }

    public Score add(Score score) {
        return new Score(points + score.points, time + score.time);
    }

    public int compareToWithTime(Score o) {
        int cmpPoints = Double.compare(points, o.points);
        if (cmpPoints == 0) {
            return -Integer.compare(time, o.time);
        }
        return cmpPoints;
    }
}
