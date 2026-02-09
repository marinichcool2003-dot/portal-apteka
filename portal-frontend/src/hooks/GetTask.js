import { useEffect, useState } from "react";
import api from "./axios";

export function useTaskById(taskId) {
    const [task, setTask] = useState()
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        const fetchTask = async () => {
            if(!taskId)
                return;
            try {
                const response = await api.get(`/task/${taskId}`)
                console.log(response.data)
                setTask(response.data)
            } catch (err) {
                setError(err.message || "Не удалось получить задачу")
            } finally {
                setLoading(false)
            }
        }
        fetchTask()
    }, [taskId])

    return {task, loading, error}
}