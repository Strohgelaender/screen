"use client";

import {useSearchParams} from "next/navigation";
import {useCallback, useEffect, useState} from "react";
import {Competition} from "../models/competition";
import {Score} from "../models/score";
import {Team} from "../models/team";
import Footer from "../components/Footer";
import {ScreenSettings} from "../models/screenSettings";
import {loadCompetition, calculateTeamsPerPage, loadScreenSettings} from "../services/ScreenService";
import ScreenContainer from "../components/ScreenContainer";

export default function ScoreScreenPage() {
    const searchParams = useSearchParams()
    const rawId = searchParams.get("id") ?? "348";
    const id = parseInt(rawId, 10);

    const [competition, setCompetition] = useState<Competition | null>(null);
    const [error, setError] = useState<string | null>(null);

    const [currentIndex, setCurrentIndex] = useState(0);
    const [isPaused, setIsPaused] = useState(false);

    const [teamsPerPage, setTeamsPerPage] = useState(8);
    const [settings, setSettings] = useState<ScreenSettings | null>(null);

    useEffect(() => {
        loadCompetition(id)
            .then((competition) => {
                setCompetition(competition);
                setTeamsPerPage(calculateTeamsPerPage(competition.categories[0]));
            })
            .catch((error) => setError(error.message));
    }, [id]);

    useEffect(() => {
        loadScreenSettings(id)
            .then((settings) => {
                setSettings(settings);
                if (settings?.teamsPerPage) {
                    setTeamsPerPage(settings.teamsPerPage);
                }
            })
            .catch((error) => console.error("Error loading settings:", error));
    }, [id]);

    const advancePage = useCallback(() => {
        setCurrentIndex((prevIndex) => {
            if (!competition || prevIndex + teamsPerPage > competition.categories[0].teams.length) return 0;
            return (prevIndex + teamsPerPage) % competition.categories[0].teams.length;
        });
    }, [competition, teamsPerPage]);

    const previousPage = useCallback(() => {
        setCurrentIndex((prevIndex) => {
            if (prevIndex === 0 && competition) {
                const teams = competition.categories[0].teams;
                const teamsLastPage = teams.length % teamsPerPage;
                return competition.categories[0]?.teams?.length - teamsLastPage;
            }
            return Math.max(prevIndex - teamsPerPage, 0);
        });
    }, [teamsPerPage]);

    useEffect(() => {
        const interval = setInterval(() => {
            if (isPaused) return;
            advancePage();
        }, 15000);

        return () => clearInterval(interval);
    }, [competition, teamsPerPage, isPaused, advancePage]);

    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Enter') {
                console.log('pausing');
                setIsPaused((prev) => !prev);
            } else if (event.key === 'ArrowRight' || event.key === 'ArrowUp') {
                advancePage();
            } else if (event.key === 'ArrowLeft' || event.key === 'ArrowDown') {
                previousPage();
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [isPaused, competition, teamsPerPage, advancePage, previousPage]);

    function renderScoreCell(score: Score, index: number) {
        const background = score.highlight ? 'blue' : 'none';
        return <td className="px-4 border-t border-r border-l border-white text-center" key={index}
                   style={{background}}>{score.points}</td>;
    }

    const teams = competition?.categories[0].teams;

    return (
        <ScreenContainer settings={settings}>
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
            <Footer settings={settings} />
        </ScreenContainer>
    );
}
