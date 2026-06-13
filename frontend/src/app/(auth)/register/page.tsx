"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { UserPlus } from "lucide-react";
import { register } from "@/lib/api/auth";
import { tokenStore } from "@/lib/auth/token-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ThemeToggle } from "@/components/theme-toggle";

const schema = z.object({
  name: z.string().min(2),
  email: z.string().email(),
  password: z.string().min(8)
});

type FormValues = z.infer<typeof schema>;

export default function RegisterPage() {
  const router = useRouter();
  const form = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { name: "", email: "", password: "" } });
  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (data) => {
      tokenStore.set(data.token);
      router.replace("/dashboard");
    }
  });

  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-background px-6">
      <div className="absolute right-6 top-6 z-20">
        <ThemeToggle />
      </div>

      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,rgba(120,120,120,0.15),transparent_45%)]" />

      <div className="relative z-10 w-full max-w-md">
        <div className="rounded-[32px] border border-border/50 bg-card/70 p-10 shadow-2xl backdrop-blur-xl">
          <div className="mb-10 text-center">
            <div className="mb-6 text-4xl font-semibold tracking-tight">
              XenoPilot
            </div>

            <h1 className="text-3xl font-semibold tracking-tight">
              Create your account
            </h1>

            <p className="mt-3 text-sm text-muted-foreground">
              Set up your workspace and start managing customers, campaigns, analytics and AI-powered audiences.
            </p>
          </div>

          <form
            className="space-y-5"
            onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
          >
            <div className="space-y-2">
              <Label htmlFor="name">Full Name</Label>
              <Input
                id="name"
                autoComplete="name"
                className="h-12 rounded-xl"
                {...form.register("name")}
              />
              {form.formState.errors.name && (
                <p className="text-sm text-destructive">
                  {form.formState.errors.name.message}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                className="h-12 rounded-xl"
                {...form.register("email")}
              />
              {form.formState.errors.email && (
                <p className="text-sm text-destructive">
                  {form.formState.errors.email.message}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                autoComplete="new-password"
                className="h-12 rounded-xl"
                {...form.register("password")}
              />
              {form.formState.errors.password && (
                <p className="text-sm text-destructive">
                  {form.formState.errors.password.message}
                </p>
              )}
            </div>

            {mutation.error && (
              <p className="text-sm text-destructive">
                {mutation.error.message}
              </p>
            )}

            <Button
              className="h-12 w-full rounded-xl text-base"
              type="submit"
              disabled={mutation.isPending}
            >
              <UserPlus className="mr-2 h-4 w-4" />
              {mutation.isPending ? "Creating account..." : "Create Account"}
            </Button>
          </form>

          <div className="mt-8 border-t pt-6 text-center">
            <p className="text-sm text-muted-foreground">
              Already registered?{' '}
              <Link
                className="font-medium text-primary hover:underline"
                href="/login"
              >
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </main>
  );
}
