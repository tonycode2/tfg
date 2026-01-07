"use client"

import * as React from "react"
import { Clock, ChevronDownIcon } from "lucide-react"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { Input } from "@/components/ui/input"

interface TimePickerProps {
  value?: string;
  onChange: (time: string) => void;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
}

export function TimePicker({
  value = "",
  onChange,
  placeholder = "Seleccionar hora",
  disabled = false,
  className,
}: TimePickerProps) {
  const [open, setOpen] = React.useState(false)
  const [hours, setHours] = React.useState<string>("")
  const [minutes, setMinutes] = React.useState<string>("")

  React.useEffect(() => {
    if (value) {
      const [h, m] = value.split(":")
      setHours(h || "")
      setMinutes(m || "")
    } else {
      setHours("")
      setMinutes("")
    }
  }, [value])

  const handleApply = () => {
    const h = hours.padStart(2, "0")
    const m = minutes.padStart(2, "0")
    const timeString = `${h}:${m}`
    onChange(timeString)
    setOpen(false)
  }

  const handlePreset = (hour: string) => {
    setHours(hour)
    setMinutes("00")
    const timeString = `${hour.padStart(2, "0")}:00`
    onChange(timeString)
    setOpen(false)
  }

  const displayValue = value || placeholder

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          disabled={disabled}
          className={cn(
            "w-full justify-between font-normal",
            !value && "text-muted-foreground",
            className
          )}
        >
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4" />
            {displayValue}
          </div>
          <ChevronDownIcon className="ml-2 h-4 w-4 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-4" align="start">
        <div className="space-y-4">
          <div className="text-sm font-medium">Seleccionar hora</div>
          
          <div className="flex gap-2 items-center">
            <div className="flex-1">
              <Input
                type="number"
                min="0"
                max="23"
                value={hours}
                onChange={(e) => {
                  const val = parseInt(e.target.value)
                  if (val >= 0 && val <= 23) {
                    setHours(e.target.value)
                  } else if (e.target.value === "") {
                    setHours("")
                  }
                }}
                placeholder="HH"
                className="text-center"
              />
            </div>
            <span className="text-xl font-bold">:</span>
            <div className="flex-1">
              <Input
                type="number"
                min="0"
                max="59"
                value={minutes}
                onChange={(e) => {
                  const val = parseInt(e.target.value)
                  if (val >= 0 && val <= 59) {
                    setMinutes(e.target.value)
                  } else if (e.target.value === "") {
                    setMinutes("")
                  }
                }}
                placeholder="MM"
                className="text-center"
              />
            </div>
          </div>

          <div className="space-y-2">
            <div className="text-xs text-muted-foreground">Presets comunes</div>
            <div className="grid grid-cols-3 gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePreset("08")}
                className="text-xs"
              >
                08:00
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePreset("09")}
                className="text-xs"
              >
                09:00
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePreset("12")}
                className="text-xs"
              >
                12:00
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePreset("13")}
                className="text-xs"
              >
                13:00
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePreset("17")}
                className="text-xs"
              >
                17:00
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePreset("18")}
                className="text-xs"
              >
                18:00
              </Button>
            </div>
          </div>

          <Button
            onClick={handleApply}
            className="w-full"
            disabled={hours === "" || minutes === ""}
          >
            Aplicar
          </Button>
        </div>
      </PopoverContent>
    </Popover>
  )
}