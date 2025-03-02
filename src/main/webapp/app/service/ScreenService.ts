import {Competition} from "../models/competition";
import {ScreenSettings} from "../models/screenSettings";
import {Category} from "../models/category";

const BASE_URL = "http://localhost:8080";

export class ScreenService {

    constructor() {
    }

    public async loadCompetition(id: number): Promise<Competition> {
                    const response = await fetch(BASE_URL + "/api/parse?id=" + id);
            const competition: Competition = await response.json();

            if (!competition?.categories?.length) {
                throw new Error("No scores found for this competition");
            }
            return competition;
    }

    public async loadQFCategory(id: number): Promise<Category> {
        const response = await fetch(BASE_URL + "/api/quarter?id=" + id);
        return await response.json();
    }

    public async loadTestround(id: number): Promise<Category> {
        const response = await fetch(BASE_URL + "/api/testround?id=" + id);
        return await response.json();
    }

    public calculateTeamsPerPage(competition: Competition): number {
        const teams = competition.categories[0].teams;
        let pages = 3;
        if (teams.length < 8) {
            pages = 1;
        } else if (teams.length < 16) {
            pages = 2;
        }
        let perPage = Math.ceil(teams.length / pages);
        let teamsLastPage = teams.length % perPage;

        while (teamsLastPage > 0 && teamsLastPage < 4 && teams.length > 8) {
            // To avoid a page with less than 4 teams, we reduce the number of teams per page
            perPage--;
            teamsLastPage = teams.length % perPage;
        }
        return perPage;
    }

    public async loadScreenSettings(id: number): Promise<ScreenSettings> {
        const response = await fetch(BASE_URL + "/api/settings?id=" + id);
        return await response.json();
    }

    public async fetchBackgroundImage(immageUrl: string): Promise<string | null> {
        try {
            const response = await fetch(BASE_URL + immageUrl);
            const blob = await response.blob();
            return URL.createObjectURL(blob);
        } catch (error) {
            console.error("Error loading background image:", error);
        }
        return null;
    }
}
