"use client";

import {useSearchParams} from "next/navigation";
import { useEffect, useState} from "react";
import {Competition} from "../models/competition";
import {Score} from "../models/score";
import {Team} from "../models/team";

export default function ScoreScreenPage() {
    const searchParams = useSearchParams()
    const rawId = searchParams.get("id") ?? "348";
    const id = parseInt(rawId, 10);

    const BASE_URL = "http://localhost:8080";


    const [competition, setCompetition] = useState<Competition | null>(null);
    const [error, setError] = useState<string | null>(null);

    const [currentIndex, setCurrentIndex] = useState(0);

    const [teamsPerPage, setTeamsPerPage] = useState(8);
    const [backgroundImage, setBackgroundImage] = useState<string | null>(null);
    const [showFooter, setShowFooter] = useState(true);
    const [footerImages, setFooterImages] = useState<string[]>([]);


    useEffect(() => {
        fetchBackgroundImage("/images/Hintergrund.png");
    }, [BASE_URL]);

    useEffect(() => {
        fetch(BASE_URL + "/api/parse?id=" + id)
            .then((response) => response.json())
            .then((competition) => {
                setCompetition(competition);
                if (!competition?.categories?.length) {
                    setError("No scores found for this competition");
                    return;
                }
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
                setTeamsPerPage(perPage);
            })
            .catch((error) => setError(error.message));
    }, [id, BASE_URL]);

    useEffect(() => {
        fetch(BASE_URL + "/api/settings?id=" + id)
            .then((response) => response.json())
            .then((settings) => {
                if (settings?.teamsPerPage) {
                    setTeamsPerPage(settings.teamsPerPage);
                }
                if (settings?.showFooter !== undefined) {
                    setShowFooter(settings.showFooter);
                }
                if (settings?.backgroundImage) {
                  fetchBackgroundImage(settings.backgroundImage);
                }
                if (settings?.footerImages) {
                    setFooterImages(settings.footerImages);
                }
            })
            .catch((error) => console.error("Error loading settings:", error));
    }, [BASE_URL]);

    useEffect(() => {
        const interval = setInterval(() => {
            setCurrentIndex((prevIndex) => {
                if (!competition || prevIndex + teamsPerPage > competition.categories[0].teams.length) return 0;
                return (prevIndex + teamsPerPage) % competition.categories[0].teams.length;
            });
        }, 15000);

        return () => clearInterval(interval);
    }, [competition, teamsPerPage]);

    function fetchBackgroundImage(immageUrl: string) {
        fetch(BASE_URL + immageUrl)
            .then((response) => response.blob())
            .then((blob) => setBackgroundImage(URL.createObjectURL(blob)))
            .catch((error) => console.error("Error loading background image:", error));
    }

    function renderScoreCell(score: Score, index: number) {
        const background = score.highlight ? 'blue' : 'none';
        return <td className="px-4 border-t border-r border-l border-white text-center" key={index}
                   style={{background}}>{score.points}</td>;
    }

    const teams = competition?.categories[0].teams;

    return (
            <div className="w-screen h-screen flex flex-col items-center justify-start bg-cover bg-center"
                 style={{
                     backgroundImage: backgroundImage ? `url(${backgroundImage})` : "none",
                 }}
            >
                <h1 className="text-white text-4xl font-bold bg-black/50 px-4 py-12 rounded-lg">
                    ROBOT-GAME SCORE: {competition?.name?.toUpperCase()}
                </h1>

                <div className="text-white text-5xl bg-black/50 rounded-lg p-20">
                    {error && <div className="text-red-500">{error}</div>}
                    <table className="w-full border-collapse table-fixed text-left text-white ">
                        <thead>
                        <tr>
                            <th className="px-4 py-2 border-b border-r border-white w-auto">Team</th>
                            <th className="px-4 py-2 border-r border-b border-white text-center w-40">R I</th>
                            <th className="px-4 py-2 border-r border-b border-white text-center w-40">R II</th>
                            <th className="px-4 py-2 border-r border-b border-white text-center w-40">R III</th>
                            <th className="px-4 py-2 border-b border-white text-center w-40">Rank</th>
                        </tr>
                        </thead>
                        <tbody>
                        {teams?.slice(currentIndex, Math.min(currentIndex + teamsPerPage, teams?.length)).map((team: Team) => (
                            <tr key={team.id}>
                                <td className="px-4 py-2 border-t border-white">{team.name}</td>
                                {team.scores.map((score, index) => renderScoreCell(score, index))}
                                <td className="px-4 py-2 border-t border-white text-center">{team.rank}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
                {showFooter &&
                <footer className="bg-white w-full flex justify-center items-center" style={{height: "15vh", position: "absolute", bottom: 0}} id={"screenFooter"}>
                    {footerImages.map((image) => (
                        <img src={BASE_URL + image} key={image} className="h-20" style={{maxHeight: "12vh"}}/>
                    ))}
                </footer> }
            </div>
    );
}
