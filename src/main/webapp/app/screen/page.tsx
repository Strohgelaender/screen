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
        return <td key={index} style={{ background }}>{score.points}</td>;
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

            <div className="text-white text-xl bg-black/50 p-4 rounded-lg">
                <table className="w-full table border-separate table-auto text-sm text-left rtl:text-center text-white">
                    <thead>
                    <tr>
                        <th>Team</th>
                        <th>R I</th>
                        <th>R II</th>
                        <th>R III</th>
                        <th>Rank</th>
                    </tr>
                    </thead>
                    <tbody>
                    {competition?.categories[0].teams.map((team: Team) => (
                        <tr key={team.id}>
                            <td>{team.name}</td>
                            {team.scores.map((score, index) => renderScoreCell(score, index))}
                            <td>{team.rank}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
