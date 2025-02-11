import {Team} from "./team";

export class Score {
    points: number;
    time: number;
    team: Team;

    constructor(points: number, time: number, team: Team) {
        this.points = points;
        this.time = time;
        this.team = team;
    }
}
