"use client";

import {useEffect, useState} from "react";
import {Competition} from "../models/competition";
import {Team} from "../models/team";
import {Score} from "../models/score";

export default function ServerDataPage() {
    const [competition, setCompetition] = useState<Competition | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [backgroundImage, setBackgroundImage] = useState<string | null>(null);

    useEffect(() => {
        fetch("http://localhost:8080/images/Hintergrund.png")
            .then((response) => response.blob())
            .then((blob) => setBackgroundImage(URL.createObjectURL(blob)))
            .catch((error) => console.error("Error loading background image:", error));
    }, []);

    useEffect(() => {
        fetch("http://localhost:8080/api/parse")
            .then((response) => response.json())
            .then(setCompetition)
            .catch((error) => setError(error.message));
    }, []);

    function renderScoreCell(score: Score, index: number) {
        const background = score.highlight ? 'blue' : 'none';
        return <td className="px-4 border-t border-r border-l border-white text-center" key={index} style={{ background }}>{score.points}</td>;
    }

    return (
        <div className="w-screen h-screen flex flex-col items-center justify-center bg-cover bg-center"
             style={{
                 backgroundImage: backgroundImage ? `url(${backgroundImage})` : "none",
             }}
        >
            <h1 className="text-white text-4xl font-bold bg-black/50 p-4 rounded-lg">
                ROBOT-GAME SCORE: {competition?.name?.toUpperCase()}
            </h1>

            <div className="text-white text-4xl bg-black/50 rounded-lg p-20">
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
                    {competition?.categories[0].teams.slice(0, 8).map((team: Team) => (
                        <tr key={team.id}>
                            <td className="px-4 py-2 border-t border-white">{team.name}</td>
                            {team.scores.map((score, index) => renderScoreCell(score, index))}
                            <td className="px-4 py-2 border-t border-white text-center">{team.rank}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
