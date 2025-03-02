"use client";

import {useEffect, useState} from "react";
import {ScreenSettings} from "../models/screenSettings";
import {useSearchParams} from "next/navigation";
import {ScreenService} from "../service/ScreenService";
import {Team} from "../models/team";
import Footer from "../components/Footer";
import {Category} from "../models/category";
import {Score} from "../models/score";
import ScreenContainer from "../components/ScreenContainer";

export default function TestroundPage() {
    const searchParams = useSearchParams()
    const rawId = searchParams.get("id") ?? "348";
    const id = parseInt(rawId, 10);

    const screenService = new ScreenService();

    const [settings, setSettings] = useState<ScreenSettings | null>(null);
    const [category, setCategory] = useState<Category | null>(null);

    const [currentIndex, setCurrentIndex] = useState(0);
    const [teamsPerPage, setTeamsPerPage] = useState(8);

    useEffect(() => {
        screenService.loadTestround(id)
            .then((category) => {
                category.teams.sort((t1, t2) => {
                    return t2.scores[0].points - t1.scores[0].points;
                });
                setTeamsPerPage(screenService.calculateTeamsPerPage(category))
                setCategory(category);
            })
            .catch((error) => console.error("Error loading category:", error));
    }, [id]);

    useEffect(() => {
        screenService.loadScreenSettings(id)
            .then((settings) => {
                setSettings(settings);
            })
            .catch((error) => console.error("Error loading settings:", error));
    }, [id]);

    useEffect(() => {
        const interval = setInterval(() => {
            setCurrentIndex((prevIndex) => {
                if (!category || prevIndex + teamsPerPage > category.teams.length) return 0;
                return (prevIndex + teamsPerPage) % category.teams.length;
            });
        }, 15000);

        return () => clearInterval(interval);
    }, [category, teamsPerPage]);

    function renderScoreCell(score: Score, index: number) {
        const background = score.highlight ? 'blue' : 'none';
        return <td className="px-4 border-t border-r border-l border-white text-center" key={index}
                   style={{background}}>{score.points}</td>;
    }

    return (
            <ScreenContainer settings={settings}>
                <h1 className="text-white text-4xl font-bold bg-black/50 px-4 py-12 rounded-lg">
                    {category?.name?.toUpperCase()}
                </h1>

                <div className="text-white text-5xl bg-black/50 rounded-lg p-20">
                    <table className="w-full border-collapse table-fixed text-left text-white ">
                        <thead>
                        <tr>
                            <th className="px-4 py-2 border-b border-r border-white w-auto">Team</th>
                            <th className="px-4 py-2 border-r border-b border-white text-center w-60">TR</th>
                            <th className="px-4 py-2 border-b border-white text-center w-40">Rank</th>
                        </tr>
                        </thead>
                        <tbody>
                        {category?.teams?.slice(currentIndex, Math.min(currentIndex + teamsPerPage, category?.teams?.length))?.map((team: Team) => (
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
