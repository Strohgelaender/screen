"use client";

import {useEffect, useState} from "react";
import {Competition} from "../models/competition";
import {Team} from "../models/team";

export default function ServerDataPage() {
    const [competition, setCompetition] = useState<Competition | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [backgroundImage, setBackgroundImage] = useState<string | null>(null);

    useEffect(() => {
        fetch("http://localhost:8080/images/Hintergrund.png")
            .then((response) => response.blob())
            .then((blob) => {
                const imageUrl = URL.createObjectURL(blob);
                setBackgroundImage(imageUrl);
            })
            .catch((error) => console.error("Error loading background image:", error));
    }, []);

    useEffect(() => {
        fetch("http://localhost:8080/api/parse")
            .then((response) => response.json())
            .then((data) => setCompetition(data))
            .catch((error) => setError(error.message));
    }, []);

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
                            <td style={{ background: team.scores[0]?.highlight ? 'blue' : 'none'}}>{team.scores[0]?.points}</td>
                            <td style={{ background: team.scores[1]?.highlight ? 'blue' : 'none'}}>{team.scores[1]?.points}</td>
                            <td style={{ background: team.scores[2]?.highlight ? 'blue' : 'none'}}>{team.scores[2]?.points}</td>
                            <td>{team.rank}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
